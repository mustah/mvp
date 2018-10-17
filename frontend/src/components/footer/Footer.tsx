import * as React from 'react';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import {componentOrNothing} from '../hoc/hocs';
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

export const Footer = componentOrNothing((_) => isProductionEnvironment)(FooterComponent);
