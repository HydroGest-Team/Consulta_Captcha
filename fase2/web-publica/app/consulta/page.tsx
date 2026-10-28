'use client';

import { useRef, useState } from 'react';
import ReCAPTCHA from 'react-google-recaptcha';

type UltimaLectura = { fecha: string; lectura: number; consumoM3: number };
type PagoResumen = { fecha: string; monto: number };
type EstadoCuenta = {
  codigoContador: string;
  titular: string;
  estado: 'AL_DIA' | 'PENDIENTE';
  saldoPendiente: number;
  ultimaLectura?: UltimaLectura | null;
  ultimosPagos: PagoResumen[];
};

const quetzales = new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'GTQ' });

function formatoFecha(iso?: string) {
  if (!iso) return '-';
  const [a, m, d] = iso.split('-');
  return `${d}/${m}/${a}`;
}

export default function PaginaConsulta() {
  const [codigo, setCodigo] = useState('');
  const [token, setToken] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const [resultado, setResultado] = useState<EstadoCuenta | null>(null);
  const [error, setError] = useState<string | null>(null);
  const captchaRef = useRef<ReCAPTCHA>(null);
  const siteKey = process.env.NEXT_PUBLIC_RECAPTCHA_SITE_KEY ?? '';

  const manejarEnvio = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setResultado(null);
    setCargando(true);

    try {
      const respuesta = await fetch('/api/consulta', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ codigo: codigo.trim().toUpperCase(), token }),
      });
      const data = await respuesta.json().catch(() => ({}));

      if (respuesta.status === 200) {
        setResultado(data as EstadoCuenta);
      } else {
        setError(data.mensaje || 'Error inesperado. Intente de nuevo');
      }
    } catch {
      setError('No se pudo conectar con el servicio');
    } finally {
      setCargando(false);
      captchaRef.current?.reset();
      setToken(null);
    }
  };

  const nuevaConsulta = () => {
    setResultado(null);
    setError(null);
    setCodigo('');
  };

  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center bg-light py-4">
      <div className="col-11 col-md-8 col-lg-5">
        <div className="text-center mb-4">
          <h1 className="h3 fw-bold mb-1">HydroGest</h1>
          <p className="text-muted mb-0">Consulta p&uacute;blica de estado de cuenta</p>
        </div>

        {!resultado && (
          <form onSubmit={manejarEnvio} className="p-4 border rounded-3 bg-white shadow-sm">
            {error && (
              <div className="alert alert-danger" role="alert">
                {error}
              </div>
            )}

            <div className="mb-3">
              <label htmlFor="codigoContador" className="form-label">
                C&oacute;digo del contador
              </label>
              <input
                id="codigoContador"
                type="text"
                className="form-control"
                value={codigo}
                onChange={(e) => {
                  setCodigo(e.target.value.toUpperCase());
                  setError(null);
                }}
                placeholder="Ej: CTR-001"
                maxLength={20}
                required
                disabled={cargando}
              />
            </div>

            <div className="mb-3 d-flex justify-content-center">
              <ReCAPTCHA ref={captchaRef} sitekey={siteKey} onChange={setToken} />
            </div>

            <button type="submit" className="btn btn-primary w-100" disabled={!codigo.trim() || !token || cargando}>
              {cargando ? 'Consultando...' : 'Consultar'}
            </button>
          </form>
        )}

        {resultado && (
          <div className="card shadow-sm" aria-live="polite">
            <div className="card-header d-flex justify-content-between align-items-center bg-white">
              <span className="fw-semibold">Contador {resultado.codigoContador}</span>
              {resultado.estado === 'AL_DIA' ? (
                <span className="badge bg-success">Al d&iacute;a</span>
              ) : (
                <span className="badge bg-warning text-dark">Pendiente</span>
              )}
            </div>

            <div className="card-body">
              <p className="text-muted small mb-1">Titular</p>
              <p className="fw-semibold mb-3">{resultado.titular}</p>

              <p className="text-muted small mb-1">Saldo pendiente</p>
              <p className={`display-6 fw-bold mb-4 ${resultado.saldoPendiente > 0 ? 'text-danger' : 'text-success'}`}>
                {quetzales.format(resultado.saldoPendiente)}
              </p>

              <h2 className="h6 fw-bold">&Uacute;ltima lectura</h2>
              {resultado.ultimaLectura ? (
                <div className="row text-center border rounded-3 py-2 mx-0 mb-4">
                  <div className="col">
                    <div className="text-muted small">Fecha</div>
                    <div className="fw-semibold">{formatoFecha(resultado.ultimaLectura.fecha)}</div>
                  </div>
                  <div className="col">
                    <div className="text-muted small">Lectura</div>
                    <div className="fw-semibold">{resultado.ultimaLectura.lectura}</div>
                  </div>
                  <div className="col">
                    <div className="text-muted small">Consumo</div>
                    <div className="fw-semibold">{resultado.ultimaLectura.consumoM3} m&sup3;</div>
                  </div>
                </div>
              ) : (
                <p className="text-muted mb-4">Sin lecturas registradas</p>
              )}

              <h2 className="h6 fw-bold">&Uacute;ltimos pagos</h2>
              {resultado.ultimosPagos.length > 0 ? (
                <table className="table table-sm mb-0">
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th className="text-end">Monto</th>
                    </tr>
                  </thead>
                  <tbody>
                    {resultado.ultimosPagos.map((p, i) => (
                      <tr key={i}>
                        <td>{formatoFecha(p.fecha)}</td>
                        <td className="text-end">{quetzales.format(p.monto)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p className="text-muted mb-0">Sin pagos registrados</p>
              )}
            </div>

            <div className="card-footer bg-white">
              <button type="button" className="btn btn-outline-primary w-100" onClick={nuevaConsulta}>
                Nueva consulta
              </button>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}