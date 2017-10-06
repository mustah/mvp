import * as classNames from 'classnames';
import * as React from 'react';
import {Selectable} from '../../../../types/Types';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {Language, supportedLanguages} from '../../languageReducer';
import {MenuSeparator} from '../separators/MenuSeparator';
import './Languages.scss';

export interface LanguagesProps {
  language: Language;
  changeLanguage: (language) => void;
}

export const Languages = (props: LanguagesProps) => {
  const {language, changeLanguage} = props;
  const {sv, en} = supportedLanguages;

  return (
    <Column className="flex-1">
      <Row className="LanguageSelect">
        <LanguageMenuItem
          language={sv}
          changeLanguage={changeLanguage}
          isSelected={language.code === sv.code}
        />
        <LanguageMenuItem
          language={en}
          changeLanguage={changeLanguage}
          isSelected={language.code === en.code}
        />
      </Row>
      <MenuSeparator/>
    </Column>
  );
};

interface LanguageMenuItemProps {
  language: Language;
  changeLanguage: (language) => void;
}

const LanguageMenuItem = (props: LanguageMenuItemProps & Selectable) => {
  const {language, changeLanguage, isSelected} = props;
  const reqChangeLanguage = () => {
    changeLanguage(language);
  };

  return (
    <div onClick={reqChangeLanguage} className={classNames('LanguageMenuItem', {isSelected})}>
      {language.code}
    </div>
  );
};
