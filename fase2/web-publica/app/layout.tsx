import './globals.css';

export const metadata = {
  title: 'HydroGest - Consulta Pública',
  description: 'Oficina municipal de agua potable - Consulta de estado de cuenta',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}