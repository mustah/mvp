import * as React from 'react';
import {Link} from 'react-router-dom';
import {getLogoPath, routes} from '../../../app/routes';
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
