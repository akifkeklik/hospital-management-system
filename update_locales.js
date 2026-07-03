const fs = require('fs');

const file = 'frontend/src/locales/index.js';
let content = fs.readFileSync(file, 'utf8');

const newKeys = {
  tr: {
    "Hastane Sistem Ayarları": "Hastane Sistem Ayarları",
    "Sistemin genel işleyiş kurallarını yönetin.": "Sistemin genel işleyiş kurallarını yönetin.",
    "Standart Randevu Süresi": "Standart Randevu Süresi",
    "Dakika": "Dakika",
    "Mesai Başlangıç": "Mesai Başlangıç",
    "Mesai Bitiş": "Mesai Bitiş",
    "Öğle Arası Başlangıç": "Öğle Arası Başlangıç",
    "Öğle Arası Bitiş": "Öğle Arası Bitiş",
    "Bakım Modu": "Bakım Modu",
    "Sistemi geçici olarak hasta erişimine kapatın.": "Sistemi geçici olarak hasta erişimine kapatın.",
    "AÇIK": "AÇIK",
    "KAPALI": "KAPALI",
    "Sistem Ayarlarını Kaydet": "Sistem Ayarlarını Kaydet"
  },
  en: {
    "Hastane Sistem Ayarları": "Hospital System Settings",
    "Sistemin genel işleyiş kurallarını yönetin.": "Manage the general operating rules of the system.",
    "Standart Randevu Süresi": "Standard Appointment Duration",
    "Dakika": "Minutes",
    "Mesai Başlangıç": "Work Start",
    "Mesai Bitiş": "Work End",
    "Öğle Arası Başlangıç": "Lunch Break Start",
    "Öğle Arası Bitiş": "Lunch Break End",
    "Bakım Modu": "Maintenance Mode",
    "Sistemi geçici olarak hasta erişimine kapatın.": "Temporarily block patient access to the system.",
    "AÇIK": "ON",
    "KAPALI": "OFF",
    "Sistem Ayarlarını Kaydet": "Save System Settings"
  },
  de: {
    "Hastane Sistem Ayarları": "Krankenhaussystem-Einstellungen",
    "Sistemin genel işleyiş kurallarını yönetin.": "Verwalten Sie die allgemeinen Betriebsregeln.",
    "Standart Randevu Süresi": "Standard-Termindauer",
    "Dakika": "Minuten",
    "Mesai Başlangıç": "Arbeitsbeginn",
    "Mesai Bitiş": "Arbeitsende",
    "Öğle Arası Başlangıç": "Mittagspause Beginn",
    "Öğle Arası Bitiş": "Mittagspause Ende",
    "Bakım Modu": "Wartungsmodus",
    "Sistemi geçici olarak hasta erişimine kapatın.": "System vorübergehend für Patienten sperren.",
    "AÇIK": "EIN",
    "KAPALI": "AUS",
    "Sistem Ayarlarını Kaydet": "Systemeinstellungen speichern"
  },
  fr: {
    "Hastane Sistem Ayarları": "Paramètres du système hospitalier",
    "Sistemin genel işleyiş kurallarını yönetin.": "Gérez les règles générales de fonctionnement.",
    "Standart Randevu Süresi": "Durée standard du rendez-vous",
    "Dakika": "Minutes",
    "Mesai Başlangıç": "Début de travail",
    "Mesai Bitiş": "Fin de travail",
    "Öğle Arası Başlangıç": "Début de pause déjeuner",
    "Öğle Arası Bitiş": "Fin de pause déjeuner",
    "Bakım Modu": "Mode maintenance",
    "Sistemi geçici olarak hasta erişimine kapatın.": "Fermer temporairement l'accès aux patients.",
    "AÇIK": "OUVERT",
    "KAPALI": "FERMÉ",
    "Sistem Ayarlarını Kaydet": "Enregistrer les paramètres"
  },
  es: {
    "Hastane Sistem Ayarları": "Configuración del Sistema",
    "Sistemin genel işleyiş kurallarını yönetin.": "Administrar reglas generales.",
    "Standart Randevu Süresi": "Duración de la cita",
    "Dakika": "Minutos",
    "Mesai Başlangıç": "Inicio de turno",
    "Mesai Bitiş": "Fin de turno",
    "Öğle Arası Başlangıç": "Inicio de almuerzo",
    "Öğle Arası Bitiş": "Fin de almuerzo",
    "Bakım Modu": "Modo de mantenimiento",
    "Sistemi geçici olarak hasta erişimine kapatın.": "Bloquear temporalmente acceso de pacientes.",
    "AÇIK": "ENCENDIDO",
    "KAPALI": "APAGADO",
    "Sistem Ayarlarını Kaydet": "Guardar configuración"
  },
  ru: {
    "Hastane Sistem Ayarları": "Настройки системы больницы",
    "Sistemin genel işleyiş kurallarını yönetin.": "Управление общими правилами системы.",
    "Standart Randevu Süresi": "Стандартная продолжительность приема",
    "Dakika": "Минут",
    "Mesai Başlangıç": "Начало работы",
    "Mesai Bitiş": "Конец работы",
    "Öğle Arası Başlangıç": "Начало обеда",
    "Öğle Arası Bitiş": "Конец обеда",
    "Bakım Modu": "Режим обслуживания",
    "Sistemi geçici olarak hasta erişimine kapatın.": "Временно закрыть доступ пациентам.",
    "AÇIK": "ВКЛ",
    "KAPALI": "ВЫКЛ",
    "Sistem Ayarlarını Kaydet": "Сохранить настройки"
  },
  ar: {
    "Hastane Sistem Ayarları": "إعدادات نظام المستشفى",
    "Sistemin genel işleyiş kurallarını yönetin.": "إدارة القواعد العامة لعمل النظام.",
    "Standart Randevu Süresi": "مدة الموعد القياسية",
    "Dakika": "دقيقة",
    "Mesai Başlangıç": "بداية الدوام",
    "Mesai Bitiş": "نهاية الدوام",
    "Öğle Arası Başlangıç": "بداية استراحة الغداء",
    "Öğle Arası Bitiş": "نهاية استراحة الغداء",
    "Bakım Modu": "وضع الصيانة",
    "Sistemi geçici olarak hasta erişimine kapatın.": "إغلاق النظام مؤقتاً أمام وصول المرضى.",
    "AÇIK": "مفتوح",
    "KAPALI": "مغلق",
    "Sistem Ayarlarını Kaydet": "حفظ إعدادات النظام"
  },
  zh: {
    "Hastane Sistem Ayarları": "医院系统设置",
    "Sistemin genel işleyiş kurallarını yönetin.": "管理系统的一般操作规则。",
    "Standart Randevu Süresi": "标准预约时间",
    "Dakika": "分钟",
    "Mesai Başlangıç": "上班时间",
    "Mesai Bitiş": "下班时间",
    "Öğle Arası Başlangıç": "午休开始",
    "Öğle Arası Bitiş": "午休结束",
    "Bakım Modu": "维护模式",
    "Sistemi geçici olarak hasta erişimine kapatın.": "暂时对患者关闭系统。",
    "AÇIK": "开",
    "KAPALI": "关",
    "Sistem Ayarlarını Kaydet": "保存系统设置"
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
    // ensure comma on the preceding line if missing, but simpler to just prepend a comma to our first key if the block has items
    // Since there are many items, we can just replace the last item without comma with a comma
    
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
