const fs = require('fs');
const path = require('path');

const localesPath = path.join(__dirname, 'frontend', 'src', 'locales', 'index.js');
let fileContent = fs.readFileSync(localesPath, 'utf8');

const translationsToAdd = {
  tr: { patient_welcome: "Sağlıklı Günler Dileriz,", patient_dashboard_subtitle: "Randevularınızı takip edin ve yönetin.", no_upcoming_appointments: "Yaklaşan Randevu Yok", no_appointments_desc: "Şu an için planlanmış bir randevunuz bulunmuyor." },
  en: { patient_welcome: "Wishing You Health,", patient_dashboard_subtitle: "Track and manage your appointments.", no_upcoming_appointments: "No Upcoming Appointments", no_appointments_desc: "You have no scheduled appointments at the moment." },
  de: { patient_welcome: "Wir wünschen Ihnen Gesundheit,", patient_dashboard_subtitle: "Verfolgen und verwalten Sie Ihre Termine.", no_upcoming_appointments: "Keine anstehenden Termine", no_appointments_desc: "Sie haben derzeit keine geplanten Termine." },
  fr: { patient_welcome: "Nous vous souhaitons une bonne santé,", patient_dashboard_subtitle: "Suivez et gérez vos rendez-vous.", no_upcoming_appointments: "Aucun rendez-vous à venir", no_appointments_desc: "Vous n'avez pas de rendez-vous prévus pour le moment." },
  es: { patient_welcome: "Le deseamos salud,", patient_dashboard_subtitle: "Rastree y administre sus citas.", no_upcoming_appointments: "No hay citas próximas", no_appointments_desc: "No tiene citas programadas en este momento." },
  ru: { patient_welcome: "Желаем вам здоровья,", patient_dashboard_subtitle: "Отслеживайте и управляйте своими приемами.", no_upcoming_appointments: "Нет предстоящих приемов", no_appointments_desc: "На данный момент у вас нет запланированных приемов." },
  ar: { patient_welcome: "نتمنى لك الصحة،", patient_dashboard_subtitle: "تتبع وإدارة مواعيدك.", no_upcoming_appointments: "لا توجد مواعيد قادمة", no_appointments_desc: "ليس لديك مواعيد مجدولة في الوقت الحالي." },
  zh: { patient_welcome: "祝您健康，", patient_dashboard_subtitle: "跟踪和管理您的预约。", no_upcoming_appointments: "没有即将到来的预约", no_appointments_desc: "您目前没有安排预约。" }
};

let match;
const regex = /([a-z]{2}):\s*\{([\s\S]*?)\}/g;
let newContent = fileContent;

while ((match = regex.exec(fileContent)) !== null) {
  const lang = match[1];
  const block = match[2];
  
  if (translationsToAdd[lang]) {
    let keysToAdd = [];
    for (const [key, value] of Object.entries(translationsToAdd[lang])) {
      if (!block.includes(`\n    ${key}:`) && !block.includes(`\n    "${key}":`) && !block.includes(` ${key}:`)) {
        keysToAdd.push(`    ${key}: "${value}"`);
      }
    }
    if (keysToAdd.length > 0) {
      const insertion = ',\n' + keysToAdd.join(',\n');
      const langRegex = new RegExp(`(${lang}:\\s*\\{[\\s\\S]*?)(\\n\\s*\\})`);
      newContent = newContent.replace(langRegex, `$1${insertion}$2`);
    }
  }
}

fs.writeFileSync(localesPath, newContent);
console.log('Translations updated.');
