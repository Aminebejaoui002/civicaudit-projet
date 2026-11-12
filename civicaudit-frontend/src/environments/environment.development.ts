// Développement (ng serve) : les appels /api passent par le proxy du serveur Angular (proxy.conf.json)
// vers le backend sur le port 8080. Le navigateur ne voit qu'une seule origine (localhost:4200).
export const environment = {
  production: false,
  apiUrl: '/api'
};
