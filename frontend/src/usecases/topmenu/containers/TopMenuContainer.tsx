import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/index';
import {routes} from '../../app/routes';
import {logout} from '../../auth/authActions';
import {AuthState} from '../../auth/authReducer';
import {Row} from '../../layouts/components/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {MainLogo} from '../components/mainlogo/MainLogo';
import {MenuItem} from '../components/menuitems/MenuItem';

export interface TopMenuContainerProps {
  pathname: string;
  auth: AuthState;
  logout: () => any;
}

const TopMenuContainer = (props: TopMenuContainerProps) => {
  const {pathname, auth, logout} = props;
  return (
    <Row className="flex-1">
      <MainLogo/>
      <Row>
        <Link to={routes.dashboard} className="link">
          <MenuItem
            name="Dashboard"
            isSelected={routes.dashboard === pathname || routes.home === pathname}
            icon="dialpad"
          />
        </Link>
        <Link to={routes.collection} className="link">
          <MenuItem name="Insamling" isSelected={routes.collection === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.validation} className="link">
          <MenuItem name="Validering" isSelected={routes.validation === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.dataAnalysis} className="link">
          <MenuItem name="Mätserier" isSelected={routes.dataAnalysis === pathname} icon="dialpad"/>
        </Link>
      </Row>
      <ProfileContainer user={auth.user} logout={logout}/>
    </Row>
  );
};

const mapStateToProps = (state: RootState) => {
  return {
    pathname: state.routing.location!.pathname,
    auth: state.auth,
    language: state.language.language,
  };
};

const mapDispatchToProps = (dispatch) => bindActionCreators({logout}, dispatch);

export default connect(mapStateToProps, mapDispatchToProps)(TopMenuContainer);
