const fs = require('fs');
let code = fs.readFileSync('frontend/src/locales/index.js', 'utf8');

const locales = ['tr', 'en', 'de', 'fr', 'es', 'ru', 'ar', 'zh'];

// Basic string replace for adding polyclinics and notifications
code = code.replace(/departments: (.*?)(,)/g, "departments: $1$2\n    polyclinics: 'Poliklinikler',\n    notifications: 'Bildirimler',");

fs.writeFileSync('frontend/src/locales/index.js', code);
console.log("Updated locales.");
