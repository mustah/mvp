import * as React from 'react';
import {colors} from '../../app/colors';
import {linkToReleaseNotes} from '../../app/routes';
import {config} from '../../config/config';
import {firstUpperTranslated} from '../../services/translationService';
import {RowCenter} from '../layouts/row/Row';
import {Link} from '../links/Link';
import {Normal} from '../texts/Texts';

const frontendVersion = config().frontendVersion;

export const Footer = () => (
  <RowCenter style={{paddingBottom: 16, marginTop: 16}} title={`${firstUpperTranslated('version')}`}>
    <Link to={linkToReleaseNotes} target="_blank" style={{color: colors.borderColor}}>
      <Normal>{frontendVersion}</Normal>
    </Link>
  </RowCenter>
);
