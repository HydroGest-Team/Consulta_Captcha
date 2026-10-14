import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form as FormBootstrap, Button, Input, Label, Alert } from 'react-bootstrap';
import { ReactComponent as RecaptchaWidget } from './recaptcha-widget.svg';
import 'bootstrap/dist/bootstrap.min.css';

export default function PaginaConsulta() {
  const [codigo, setCodigo] = useState('');
  const [captchaResuelto, setCaptchaResuelto] = useState(false);
  const [cargando, setCargando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Reiniciar captcha después de cada envío
  useEffect(() => {
    if (resultado || error) {
      setTimeout(() => setCaptchaResuelto(false), 100);
    }
  }, [resultado, error]);

  const manejarEnvio = async (e: React.MouseEvent) => {
    e.preventDefault();
    setError(null);
    setCargando(true);

    try {
      const respuesta = await fetch('/api/consulta', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ codigo, token: captchaResuelto ? 'resuelto' : '' }),
      });

      const data = await respuesta.json();

      if (respuesta.status === 200) {
        setResultado(data);
      } else if (respuesta.status === 404) {
        setError(data.mensaje || 'Contador no encontrado');
      } else if (respuesta.status === 429) {
        setError(data.mensaje || 'Demasiados intentos. Inténtalo más tarde');
      } else if (respuesta.status === 400) {
        setError(data.mensaje || 'Solicitud inválida');
      } else {
        setError('Error inesperado. Inténtalo de nuevo');
      }
    } catch (err) {
      setError('No se pudo conectar con el servicio');
    } finally {
      setCargando(false);
    }
  };

  const manejarCambioCodigo = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCodigo(e.target.value);
    setError(null);
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center">
      <div className="col-md-8 col-lg-5 p-4">
        <FormBootstrap onSubmit={manejarEnvio} className="p-4 border rounded-3">
          <h2 className="text-center mb-4 fw-bold">HydroGest - Consulta Pública</h2>

          {error && (
            <Alert variant="danger" className="mb-3">
              {error}
            </Alert>
          )}

          <div className="mb-3">
            <Label htmlFor="codigoContador">Código del contador</Label>
            <Input
              id="codigoContador"
              type="text"
              value={codigo}
              onChange={manejarCambioCodigo}
              placeholder="Ej: HGC-2024-001"
              required
              disabled={cargando}
              className={`
                width: 100%;
                padding: 10px;
                font-size: 1rem;
                border: 1px solid #ced4da;
                border-radius: 4px;
                box-sizing: border-box;
              `}
            />
          </div>

          {captchaResuelto ? null : (
            <div className="mb-3">
              <Label>Verificación de seguridad</Label>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <span>Selecciona "No soy un robot"</span>
                <button
                  onClick={() => setCaptchaResuelto(true)}
                  className="btn btn-link p-0 fs-6 text-primary"
                  disabled={cargando}
                >
                  Verificar
                </button>
              </div>
            </div>
          )}

          <Button type="submit" disabled={!codigo || !captchaResuelto || cargando}>
            {cargando ? 'Consultando...' : 'Consultar'}
          </Button>
        </FormBootstrap>
      </div>
    </div>
  );
}