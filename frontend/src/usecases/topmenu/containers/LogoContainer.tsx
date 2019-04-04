import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {getLogoPath, routes} from '../../../app/routes';
import {Logo} from '../../../components/logo/Logo';
import {RootState} from '../../../reducers/rootReducer';
import {getOrganisationSlug} from '../../auth/authSelectors';
import './LogoContainer.scss';

interface StateToProps {
  organisationSlug: string;
}

const LogoComponent = ({organisationSlug}: StateToProps) => (
  <Link className="Logo" to={routes.home}>
    <Logo src={getLogoPath(organisationSlug)} className="small"/>
  </Link>
);

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  organisationSlug: getOrganisationSlug(auth),
});

export const LogoContainer = connect<StateToProps>(mapStateToProps)(LogoComponent);
