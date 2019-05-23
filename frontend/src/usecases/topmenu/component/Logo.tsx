import * as React from 'react';
import {getLogoPath, routes} from '../../../app/routes';
import {Link} from '../../../components/links/Link';
import {Logo as LogoImg} from '../../../components/logo/Logo';
import './Logo.scss';

export interface StateToProps {
  slug: string;
}

export const Logo = ({slug}: StateToProps) => (
  <Link className="Logo" to={routes.home}>
    <LogoImg src={getLogoPath(slug)} className="small"/>
  </Link>
);
