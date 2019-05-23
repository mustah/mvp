import * as React from 'react';
import {linkToReleaseNotes} from '../../app/routes';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import {RowCenter} from '../layouts/row/Row';
import {Link} from '../links/Link';
import {Normal} from '../texts/Texts';
import './Footer.scss';

const frontendVersion = config().frontendVersion;

export const Footer = () => (
  <RowCenter className="Footer">
    <Link to={linkToReleaseNotes} target="_blank">
      <Normal>{`${firstUpperTranslated('version')}: ${frontendVersion}`}</Normal>
    </Link>
  </RowCenter>
);
