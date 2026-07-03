const fs = require('fs');
const path = require('path');

const localesPath = path.join(__dirname, 'frontend', 'src', 'locales', 'index.js');
let fileContent = fs.readFileSync(localesPath, 'utf8');

const translationsToAdd = {
  tr: { 
    global_search_placeholder: "Ara... (Örn: Doktor, Bölüm)", hero_welcome: "Sizlere Sağlıklı Günler Dileriz!", hero_search_placeholder: "Hangi bölüme veya kime görünmek istersiniz?", filter_all_time: "Tüm Zamanlar", filter_today: "Bugün", filter_this_week: "Bu Hafta (7 Gün)", filter_this_month: "Bu Ay (30 Gün)", filter_three_months: "Son 3 Ay", filter_six_months: "Son 6 Ay", empty_state_title: "Veri Bulunamadı", empty_state_desc: "Şu an için gösterilecek herhangi bir kayıt yok."
  },
  en: { 
    global_search_placeholder: "Search... (e.g. Doctor, Dept)", hero_welcome: "Wishing You Healthy Days!", hero_search_placeholder: "Which department or doctor are you looking for?", filter_all_time: "All Time", filter_today: "Today", filter_this_week: "This Week (7 Days)", filter_this_month: "This Month (30 Days)", filter_three_months: "Last 3 Months", filter_six_months: "Last 6 Months", empty_state_title: "No Data Found", empty_state_desc: "There are no records to display at the moment."
  },
  de: { 
    global_search_placeholder: "Suchen... (z.B. Arzt, Abt)", hero_welcome: "Wir wünschen Ihnen gesunde Tage!", hero_search_placeholder: "Welche Abteilung oder welchen Arzt suchen Sie?", filter_all_time: "Gesamte Zeit", filter_today: "Heute", filter_this_week: "Diese Woche (7 Tage)", filter_this_month: "Dieser Monat (30 Tage)", filter_three_months: "Letzte 3 Monate", filter_six_months: "Letzte 6 Monate", empty_state_title: "Keine Daten gefunden", empty_state_desc: "Derzeit gibt es keine Aufzeichnungen anzuzeigen."
  },
  fr: { 
    global_search_placeholder: "Rechercher... (ex: Docteur, Dép)", hero_welcome: "Nous vous souhaitons des jours en bonne santé!", hero_search_placeholder: "Quel département ou médecin recherchez-vous?", filter_all_time: "Tout le temps", filter_today: "Aujourd'hui", filter_this_week: "Cette semaine (7 jours)", filter_this_month: "Ce mois-ci (30 jours)", filter_three_months: "Les 3 derniers mois", filter_six_months: "Les 6 derniers mois", empty_state_title: "Aucune donnée trouvée", empty_state_desc: "Il n'y a aucun enregistrement à afficher pour le moment."
  },
  es: { 
    global_search_placeholder: "Buscar... (ej. Doctor, Depto)", hero_welcome: "¡Le deseamos días saludables!", hero_search_placeholder: "¿Qué departamento o médico busca?", filter_all_time: "Todo el tiempo", filter_today: "Hoy", filter_this_week: "Esta semana (7 días)", filter_this_month: "Este mes (30 días)", filter_three_months: "Últimos 3 meses", filter_six_months: "Últimos 6 meses", empty_state_title: "Datos no encontrados", empty_state_desc: "No hay registros para mostrar en este momento."
  },
  ru: { 
    global_search_placeholder: "Поиск... (например, врач, отд)", hero_welcome: "Желаем вам здоровых дней!", hero_search_placeholder: "Какое отделение или врача вы ищете?", filter_all_time: "Все время", filter_today: "Сегодня", filter_this_week: "Эта неделя (7 дней)", filter_this_month: "Этот месяц (30 дней)", filter_three_months: "Последние 3 месяца", filter_six_months: "Последние 6 месяцев", empty_state_title: "Данные не найдены", empty_state_desc: "На данный момент нет записей для отображения."
  },
  ar: { 
    global_search_placeholder: "بحث... (مثلاً طبيب، قسم)", hero_welcome: "نتمنى لك أياماً صحية!", hero_search_placeholder: "ما القسم أو الطبيب الذي تبحث عنه؟", filter_all_time: "كل الوقت", filter_today: "اليوم", filter_this_week: "هذا الأسبوع (7 أيام)", filter_this_month: "هذا الشهر (30 يوماً)", filter_three_months: "آخر 3 أشهر", filter_six_months: "آخر 6 أشهر", empty_state_title: "لم يتم العثور على بيانات", empty_state_desc: "لا توجد سجلات لعرضها في الوقت الحالي."
  },
  zh: { 
    global_search_placeholder: "搜索... (如：医生，科室)", hero_welcome: "祝您身体健康！", hero_search_placeholder: "您在寻找哪个科室或医生？", filter_all_time: "所有时间", filter_today: "今天", filter_this_week: "本周（7天）", filter_this_month: "本月（30天）", filter_three_months: "过去3个月", filter_six_months: "过去6个月", empty_state_title: "未找到数据", empty_state_desc: "目前没有可显示的记录。"
  }
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
