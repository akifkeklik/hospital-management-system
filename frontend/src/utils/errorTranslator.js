/**
 * 🌐 Error Message Translator
 * 
 * Backend (Spring Boot) her zaman Türkçe hata mesajları döndürür.
 * Bu modül, backend'den gelen ham hata mesajlarını kullanıcının
 * seçtiği dile çevirir.
 * 
 * Eğer mesaj eşleşmezse, orijinal mesajı olduğu gibi döndürür.
 */

const errorTranslations = {
  // ── AUTH / REGISTER / LOGIN ──
  "Hata: Bu TC Kimlik Numarası zaten kayıtlı.": {
    tr: "Bu TC Kimlik Numarası zaten kayıtlı.",
    en: "This ID number is already registered.",
    de: "Diese ID-Nummer ist bereits registriert.",
    fr: "Ce numéro d'identité est déjà enregistré.",
    es: "Este número de identidad ya está registrado.",
    ru: "Этот идентификационный номер уже зарегистрирован.",
    ar: "رقم الهوية هذا مسجل بالفعل.",
    zh: "此身份证号码已注册。",
    ja: "このID番号はすでに登録されています。",
    ko: "이 ID 번호는 이미 등록되어 있습니다."
  },
  "Hata: Bu e-posta adresi zaten kullanılıyor.": {
    tr: "Bu e-posta adresi zaten kullanılıyor.",
    en: "This email address is already in use.",
    de: "Diese E-Mail-Adresse wird bereits verwendet.",
    fr: "Cette adresse e-mail est déjà utilisée.",
    es: "Esta dirección de correo electrónico ya está en uso.",
    ru: "Этот адрес электронной почты уже используется.",
    ar: "عنوان البريد الإلكتروني هذا مستخدم بالفعل.",
    zh: "此电子邮件地址已在使用中。",
    ja: "このメールアドレスはすでに使用されています。",
    ko: "이 이메일 주소는 이미 사용 중입니다."
  },
  "Hata: Şifreniz en az 6 karakter olmalıdır.": {
    tr: "Şifreniz en az 6 karakter olmalıdır.",
    en: "Your password must be at least 6 characters.",
    de: "Ihr Passwort muss mindestens 6 Zeichen lang sein.",
    fr: "Votre mot de passe doit comporter au moins 6 caractères.",
    es: "Su contraseña debe tener al menos 6 caracteres.",
    ru: "Ваш пароль должен содержать не менее 6 символов.",
    ar: "يجب أن تتكون كلمة المرور من 6 أحرف على الأقل.",
    zh: "您的密码必须至少为6个字符。",
    ja: "パスワードは6文字以上でなければなりません。",
    ko: "비밀번호는 최소 6자 이상이어야 합니다."
  },
  "Giriş bilgileri hatalı.": {
    tr: "Giriş bilgileri hatalı. Lütfen TC Kimlik No ve şifrenizi kontrol edin.",
    en: "Login credentials are incorrect. Please check your ID number and password.",
    de: "Anmeldedaten sind falsch. Bitte überprüfen Sie Ihre ID-Nummer und Ihr Passwort.",
    fr: "Les identifiants de connexion sont incorrects. Veuillez vérifier votre numéro d'identité et votre mot de passe.",
    es: "Las credenciales de inicio de sesión son incorrectas. Verifique su número de identidad y contraseña.",
    ru: "Неверные учетные данные. Пожалуйста, проверьте свой идентификационный номер и пароль.",
    ar: "بيانات الاعتماد غير صحيحة. يرجى التحقق من رقم الهوية وكلمة المرور.",
    zh: "登录凭据不正确。请检查您的身份证号码和密码。",
    ja: "ログイン情報が正しくありません。ID番号とパスワードを確認してください。",
    ko: "로그인 자격 증명이 잘못되었습니다. ID 번호와 비밀번호를 확인하세요."
  },
  "Hata: Girdiğiniz TC Kimlik Numarası veya E-Posta adresi sistemimizle eşleşmiyor.": {
    tr: "Girdiğiniz TC Kimlik Numarası veya E-Posta adresi sistemimizle eşleşmiyor.",
    en: "The ID number or email address you entered does not match our records.",
    de: "Die eingegebene ID-Nummer oder E-Mail-Adresse stimmt nicht mit unseren Aufzeichnungen überein.",
    fr: "Le numéro d'identité ou l'adresse e-mail que vous avez saisi ne correspond pas à nos enregistrements.",
    es: "El número de identidad o la dirección de correo electrónico que ingresó no coincide con nuestros registros.",
    ru: "Введенный идентификационный номер или адрес электронной почты не совпадает с нашими записями.",
    ar: "رقم الهوية أو عنوان البريد الإلكتروني الذي أدخلته لا يتطابق مع سجلاتنا.",
    zh: "您输入的身份证号码或电子邮件地址与我们的记录不匹配。",
    ja: "入力されたID番号またはメールアドレスが登録情報と一致しません。",
    ko: "입력한 ID 번호 또는 이메일 주소가 기록과 일치하지 않습니다."
  },
  "Hata: Yeni şifreniz en az 6 karakter olmalıdır.": {
    tr: "Yeni şifreniz en az 6 karakter olmalıdır.",
    en: "Your new password must be at least 6 characters.",
    de: "Ihr neues Passwort muss mindestens 6 Zeichen lang sein.",
    fr: "Votre nouveau mot de passe doit comporter au moins 6 caractères.",
    es: "Su nueva contraseña debe tener al menos 6 caracteres.",
    ru: "Ваш новый пароль должен содержать не менее 6 символов.",
    ar: "يجب أن تتكون كلمة المرور الجديدة من 6 أحرف على الأقل.",
    zh: "您的新密码必须至少为6个字符。",
    ja: "新しいパスワードは6文字以上でなければなりません。",
    ko: "새 비밀번호는 최소 6자 이상이어야 합니다."
  },
  "Hata: Şifre değiştirilemedi.": {
    tr: "Şifre değiştirilemedi.",
    en: "Password could not be changed.",
    de: "Das Passwort konnte nicht geändert werden.",
    fr: "Le mot de passe n'a pas pu être modifié.",
    es: "No se pudo cambiar la contraseña.",
    ru: "Не удалось изменить пароль.",
    ar: "لم يتمكن من تغيير كلمة المرور.",
    zh: "无法更改密码。",
    ja: "パスワードを変更できませんでした。",
    ko: "비밀번호를 변경할 수 없습니다."
  },
  "Hata: Bu kullanıcı için şifre sıfırlama zorunluluğu bulunmuyor veya kullanıcı geçersiz.": {
    tr: "Bu kullanıcı için şifre sıfırlama zorunluluğu bulunmuyor veya kullanıcı geçersiz.",
    en: "No password reset requirement found for this user, or the user is invalid.",
    de: "Kein Passwort-Reset erforderlich für diesen Benutzer oder der Benutzer ist ungültig.",
    fr: "Aucune obligation de réinitialisation de mot de passe trouvée pour cet utilisateur, ou l'utilisateur est invalide.",
    es: "No se encontró requisito de restablecimiento de contraseña para este usuario, o el usuario es inválido.",
    ru: "Для этого пользователя не найдено требование сброса пароля, или пользователь недействителен.",
    ar: "لا يوجد متطلب لإعادة تعيين كلمة المرور لهذا المستخدم، أو المستخدم غير صالح.",
    zh: "未找到此用户的密码重置要求，或用户无效。",
    ja: "このユーザーのパスワードリセット要件が見つからないか、ユーザーが無効です。",
    ko: "이 사용자의 비밀번호 재설정 요구 사항을 찾을 수 없거나 사용자가 잘못되었습니다."
  },

  // ── NETWORK / GENERIC ERRORS ──
  "Failed to fetch": {
    tr: "Sunucuya bağlanılamadı. Lütfen internet bağlantınızı kontrol edin.",
    en: "Could not connect to the server. Please check your internet connection.",
    de: "Verbindung zum Server fehlgeschlagen. Bitte überprüfen Sie Ihre Internetverbindung.",
    fr: "Impossible de se connecter au serveur. Veuillez vérifier votre connexion Internet.",
    es: "No se pudo conectar al servidor. Verifique su conexión a Internet.",
    ru: "Не удалось подключиться к серверу. Пожалуйста, проверьте подключение к Интернету.",
    ar: "تعذر الاتصال بالخادم. يرجى التحقق من اتصالك بالإنترنت.",
    zh: "无法连接到服务器。请检查您的互联网连接。",
    ja: "サーバーに接続できませんでした。インターネット接続を確認してください。",
    ko: "서버에 연결할 수 없습니다. 인터넷 연결을 확인하세요."
  },
  "NetworkError when attempting to fetch resource.": {
    tr: "Ağ hatası oluştu. Lütfen internet bağlantınızı kontrol edin.",
    en: "A network error occurred. Please check your internet connection.",
    de: "Ein Netzwerkfehler ist aufgetreten. Bitte überprüfen Sie Ihre Internetverbindung.",
    fr: "Une erreur réseau s'est produite. Veuillez vérifier votre connexion Internet.",
    es: "Se produjo un error de red. Verifique su conexión a Internet.",
    ru: "Произошла сетевая ошибка. Пожалуйста, проверьте подключение к Интернету.",
    ar: "حدث خطأ في الشبكة. يرجى التحقق من اتصالك بالإنترنت.",
    zh: "发生网络错误。请检查您的互联网连接。",
    ja: "ネットワークエラーが発生しました。インターネット接続を確認してください。",
    ko: "네트워크 오류가 발생했습니다. 인터넷 연결을 확인하세요."
  }
};

/**
 * Backend'den gelen ham hata mesajını kullanıcının diline çevirir.
 * @param {string} rawMessage - Backend'den gelen veya browser'ın ürettiği hata mesajı
 * @param {string} locale - Kullanıcının seçtiği dil kodu (ör: 'tr', 'en', 'de')
 * @returns {string} - Çevrilmiş hata mesajı veya orijinal mesaj
 */
export function translateError(rawMessage, locale = 'tr') {
  if (!rawMessage) return '';
  
  const msg = rawMessage.trim();
  
  // Exact match
  if (errorTranslations[msg] && errorTranslations[msg][locale]) {
    return errorTranslations[msg][locale];
  }
  
  // Partial match (backend bazen prefix ekleyebilir)
  for (const [key, translations] of Object.entries(errorTranslations)) {
    if (msg.includes(key) && translations[locale]) {
      return translations[locale];
    }
  }
  
  // Eğer çeviri bulunamazsa orijinal mesajı döndür
  return msg;
}
