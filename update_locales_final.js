const fs = require('fs');

const file = 'frontend/src/locales/index.js';
let content = fs.readFileSync(file, 'utf8');

const replacements = {
  ar: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "العيادات الشاملة"',
    "notifications: 'Bildirimler'": 'notifications: "الإشعارات"',
    "Hiç bildiriminiz yok.": "ليس لديك أي إشعارات.",
    "Tüm Bildirimleri Gör": "عرض كل الإشعارات",
    "Bildirim Merkezi": "مركز الإشعارات",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "تتبع أحدث الحالات والمعلومات."
  },
  en: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "Polyclinics"',
    "notifications: 'Bildirimler'": 'notifications: "Notifications"',
    "Hiç bildiriminiz yok.": "You have no notifications.",
    "Tüm Bildirimleri Gör": "See all notifications",
    "Bildirim Merkezi": "Notification Center",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Track current statuses and information."
  },
  de: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "Polikliniken"',
    "notifications: 'Bildirimler'": 'notifications: "Benachrichtigungen"',
    "Hiç bildiriminiz yok.": "Sie haben keine Benachrichtigungen.",
    "Tüm Bildirimleri Gör": "Alle Benachrichtigungen anzeigen",
    "Bildirim Merkezi": "Benachrichtigungszentrum",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Verfolgen Sie aktuelle Status und Informationen."
  },
  fr: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "Polycliniques"',
    "notifications: 'Bildirimler'": 'notifications: "Notifications"',
    "Hiç bildiriminiz yok.": "Vous n'avez aucune notification.",
    "Tüm Bildirimleri Gör": "Voir toutes les notifications",
    "Bildirim Merkezi": "Centre de notifications",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Suivez les statuts et informations actuels."
  },
  es: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "Policlínicas"',
    "notifications: 'Bildirimler'": 'notifications: "Notificaciones"',
    "Hiç bildiriminiz yok.": "No tienes notificaciones.",
    "Tüm Bildirimleri Gör": "Ver todas las notificaciones",
    "Bildirim Merkezi": "Centro de notificaciones",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Rastree los estados e información actuales."
  },
  ru: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "Поликлиники"',
    "notifications: 'Bildirimler'": 'notifications: "Уведомления"',
    "Hiç bildiriminiz yok.": "У вас нет уведомлений.",
    "Tüm Bildirimleri Gör": "Смотреть все уведомления",
    "Bildirim Merkezi": "Центр уведомлений",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Отслеживайте текущие статусы и информацию."
  },
  zh: {
    "polyclinics: 'Poliklinikler'": 'polyclinics: "综合诊所"',
    "notifications: 'Bildirimler'": 'notifications: "通知"',
    "Hiç bildiriminiz yok.": "您没有通知。",
    "Tüm Bildirimleri Gör": "查看所有通知",
    "Bildirim Merkezi": "通知中心",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "跟踪当前状态和信息。"
  },
  tr: {
    "Hiç bildiriminiz yok.": "Hiç bildiriminiz yok.",
    "Tüm Bildirimleri Gör": "Tüm Bildirimleri Gör",
    "Bildirim Merkezi": "Bildirim Merkezi",
    "Güncel durumları ve bilgilendirmeleri takip edin.": "Güncel durumları ve bilgilendirmeleri takip edin."
  }
};

for (const lang of Object.keys(replacements)) {
  const langRegex = new RegExp(`(\\b${lang}:\\s*\\{[\\s\\S]*?)(^\\s*\\})`, 'm');
  const match = content.match(langRegex);
  
  if (match) {
    let langBlock = match[1];
    
    // Replace incorrectly translated keys if they exist in the block
    for (const [k, v] of Object.entries(replacements[lang])) {
      if (k.includes(':')) {
        langBlock = langBlock.replace(k, v);
      } else {
        langBlock = langBlock.trimEnd();
        if (!langBlock.endsWith(',')) {
          langBlock += ',';
        }
        langBlock += `\n    "${k}": "${v}",\n`;
      }
    }
    
    content = content.replace(match[1], langBlock);
  }
}

fs.writeFileSync(file, content, 'utf8');
console.log('Final missing locales updated successfully!');
