'use client';

import { useRef, useState } from 'react';
import ReCAPTCHA from 'react-google-recaptcha';

export default function PaginaConsulta() {
  const [codigo, setCodigo] = useState('');
  const [token, setToken] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const [resultado, setResultado] = useState<unknown>(null);
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
        setResultado(data);
      } else if (respuesta.status === 404) {
        setError(data.mensaje || 'Contador no encontrado');
      } else if (respuesta.status === 429) {
        setError(data.mensaje || 'Demasiados intentos. Inténtalo más tarde');
      } else if (respuesta.status === 400) {
        setError(data.mensaje || 'Solicitud inválida');
      } else {
        setError(data.mensaje || 'Error inesperado. Inténtalo de nuevo');
      }
    } catch {
      setError('No se pudo conectar con el servicio');
    } finally {
      setCargando(false);
      captchaRef.current?.reset();
      setToken(null);
    }
  };

  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="col-md-8 col-lg-5 p-3">
        <form onSubmit={manejarEnvio} className="p-4 border rounded-3 bg-white">
          <h1 className="h3 text-center mb-4 fw-bold">HydroGest - Consulta Pública</h1>

          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}

          <div className="mb-3">
            <label htmlFor="codigoContador" className="form-label">
              Código del contador
            </label>
            <input
              id="codigoContador"
              type="text"
              className="form-control"
              value={codigo}
              onChange={(e) => {
                setCodigo(e.target.value);
                setError(null);
              }}
              placeholder="Ej: HGC-2024-001"
              maxLength={20}
              required
              disabled={cargando}
            />
          </div>

          <div className="mb-3">
            <ReCAPTCHA ref={captchaRef} sitekey={siteKey} onChange={setToken} />
          </div>

          <button
            type="submit"
            className="btn btn-primary w-100"
            disabled={!codigo.trim() || !token || cargando}
          >
            {cargando ? 'Consultando...' : 'Consultar'}
          </button>
        </form>

        {resultado !== null && (
          <div className="card mt-4" aria-live="polite">
            <div className="card-body">
              <h2 className="h5 card-title">Estado de cuenta</h2>
              <pre className="mb-0 small">{JSON.stringify(resultado, null, 2)}</pre>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}