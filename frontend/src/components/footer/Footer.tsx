import * as React from 'react';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import {componentOrNull} from '../hoc/hocs';
import {RowCenter} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './Footer.scss';

const frontendVersion = config().frontendVersion;
const isProductionEnvironment = config().environment === 'production';

const FooterComponent = () => (
  <RowCenter className="Footer">
    <Normal>{firstUpperTranslated('version')} {frontendVersion}</Normal>
  </RowCenter>
);

export const Footer = componentOrNull((_) => isProductionEnvironment)(FooterComponent);
