const defaultLanguage = 'en';
const defaultNamespace = 'translation';

const i18nextConfig = {
  defaultNs: defaultNamespace,
  fallbackLng: defaultLanguage,
  interpolation: {
    escapeValue: false,
    formatSeparator: ',',
    prefix: '{{',
    suffix: '}}',
  },
  contextSeparator: '°_°',
  keySeparator: '°.°',
  nsSeparator: '°:°',
  pluralSeparator: '°_°',
};

module.exports = {i18nextConfig, defaultNamespace, defaultLanguage};
