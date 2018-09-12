import * as React from 'react';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import {componentOrNull} from '../hoc/hocs';
import {RowCenter} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './Footer.scss';

const version: string = config().frontendVersion;
// keep the placeholder string separated, so that it itself is not replaced in the build process
const wasReplaced: boolean = version !== 'FRONTEND' + '_VERSION';

export const FooterComponent = () => (
  <RowCenter className="Footer">
    <Normal>{firstUpperTranslated('version')} {version}</Normal>
  </RowCenter>
);

export const Footer = componentOrNull((_) => wasReplaced)(FooterComponent);
