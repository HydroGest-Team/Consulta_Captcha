import Image from 'next/image';
import Link from 'next/link';
import 'bootstrap/dist/bootstrap.min.css';

export default function Home() {
  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
      <div className="text-center">
        <h1 className="fw-bold mb-3">HydroGest</h1>
        <p className="text-muted mb-4">Oficina municipal de agua potable</p>
        <div>
          <Link href="/consulta" passHref>
            <Button variant="primary" size="lg">
              Iniciar Consulta Pública
            </Button>
          </Link>
        </div>
        <p className="mt-4 small text-muted">
          Versión 1.0 - Fase 2
        </p>
      </div>
    </div>
  );
}