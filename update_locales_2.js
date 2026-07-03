const fs = require('fs');

const file = 'frontend/src/locales/index.js';
let content = fs.readFileSync(file, 'utf8');

const newKeys = {
  tr: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "Duyuru tüm hekimlere başarıyla gönderildi!",
    "Duyuru gönderilemedi.": "Duyuru gönderilemedi.",
    "Bildirimler yükleniyor...": "Bildirimler yükleniyor...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "Şu an için yeni bir bildiriminiz bulunmuyor.",
    "YENİ": "YENİ",
    "Okundu": "Okundu"
  },
  en: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "Announcement sent to all doctors successfully!",
    "Duyuru gönderilemedi.": "Failed to send announcement.",
    "Bildirimler yükleniyor...": "Loading notifications...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "You don't have any new notifications at the moment.",
    "YENİ": "NEW",
    "Okundu": "Read"
  },
  de: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "Die Ankündigung wurde erfolgreich an alle Ärzte gesendet!",
    "Duyuru gönderilemedi.": "Die Ankündigung konnte nicht gesendet werden.",
    "Bildirimler yükleniyor...": "Benachrichtigungen werden geladen...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "Sie haben momentan keine neuen Benachrichtigungen.",
    "YENİ": "NEU",
    "Okundu": "Gelesen"
  },
  fr: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "Annonce envoyée à tous les médecins avec succès !",
    "Duyuru gönderilemedi.": "Échec de l'envoi de l'annonce.",
    "Bildirimler yükleniyor...": "Chargement des notifications...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "Vous n'avez pas de nouvelles notifications pour le moment.",
    "YENİ": "NOUVEAU",
    "Okundu": "Lu"
  },
  es: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "¡El anuncio se envió a todos los médicos con éxito!",
    "Duyuru gönderilemedi.": "Error al enviar el anuncio.",
    "Bildirimler yükleniyor...": "Cargando notificaciones...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "No tienes nuevas notificaciones por el momento.",
    "YENİ": "NUEVO",
    "Okundu": "Leído"
  },
  ru: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "Объявление успешно отправлено всем врачам!",
    "Duyuru gönderilemedi.": "Не удалось отправить объявление.",
    "Bildirimler yükleniyor...": "Загрузка уведомлений...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "На данный момент у вас нет новых уведомлений.",
    "YENİ": "НОВОЕ",
    "Okundu": "Прочитано"
  },
  ar: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "تم إرسال الإعلان لجميع الأطباء بنجاح!",
    "Duyuru gönderilemedi.": "فشل في إرسال الإعلان.",
    "Bildirimler yükleniyor...": "جاري تحميل الإشعارات...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "ليس لديك أي إشعارات جديدة في الوقت الحالي.",
    "YENİ": "جديد",
    "Okundu": "مقروء"
  },
  zh: {
    "Duyuru tüm hekimlere başarıyla gönderildi!": "公告已成功发送给所有医生！",
    "Duyuru gönderilemedi.": "发送公告失败。",
    "Bildirimler yükleniyor...": "正在加载通知...",
    "Şu an için yeni bir bildiriminiz bulunmuyor.": "目前没有新通知。",
    "YENİ": "新",
    "Okundu": "已读"
  }
};

for (const lang of Object.keys(newKeys)) {
  const langRegex = new RegExp(`(\\b${lang}:\\s*\\{[\\s\\S]*?)(^\\s*\\})`, 'm');
  const match = content.match(langRegex);
  
  if (match) {
    let toInsert = '';
    for (const [k, v] of Object.entries(newKeys[lang])) {
      toInsert += `    "${k}": "${v}",\n`;
    }
    
    content = content.replace(langRegex, (full, p1, p2) => {
      let cleanP1 = p1.trimEnd();
      if (!cleanP1.endsWith(',')) {
        cleanP1 += ',';
      }
      return cleanP1 + '\\n' + toInsert + p2;
    });
  }
}

fs.writeFileSync(file, content, 'utf8');
console.log('Locales updated successfully!');
