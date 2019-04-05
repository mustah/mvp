import * as React from 'react';
import {linkToReleaseNotes} from '../../app/routes';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import '../buttons/ButtonLink.scss';
import {RowCenter} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './Footer.scss';

const frontendVersion = config().frontendVersion;

export const Footer = () => (
  <RowCenter className="Footer">
    <a href={linkToReleaseNotes} target="_blank" className="link">
      <Normal>{`${firstUpperTranslated('version')}: ${frontendVersion}`}</Normal>
    </a>
  </RowCenter>
);
