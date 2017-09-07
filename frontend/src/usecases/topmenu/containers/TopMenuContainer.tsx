import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {RootState} from '../../../reducers/index';
import {routes} from '../../app/routes';
import {AuthState} from '../../auth/authReducer';
import {Row} from '../../layouts/components/row/Row';
import {ProfileContainer} from '../../profile/containers/ProfileContainer';
import {MainLogo} from '../components/mainlogo/MainLogo';
import {MenuItem} from '../components/menuitems/MenuItem';

export interface TopMenuContainerProps {
  pathname: string;
  auth: AuthState;
}

const TopMenuContainer = (props: TopMenuContainerProps) => {
  const {pathname, auth} = props;
  return (
    <Row className="flex-1">
      <MainLogo/>
      <Row>
        <Link to={routes.dashboard} replace={true} className="link">
          <MenuItem
            name="Dashboard"
            isSelected={routes.dashboard === pathname || routes.home === pathname}
            icon="dialpad"
          />
        </Link>
        <Link to={routes.collection} replace={true} className="link">
          <MenuItem name="Insamling" isSelected={routes.collection === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.validation} className="link">
          <MenuItem name="Validering" isSelected={routes.validation === pathname} icon="dialpad"/>
        </Link>
        <Link to={routes.dataAnalysis} className="link">
          <MenuItem name="Mätserier" isSelected={routes.dataAnalysis === pathname} icon="dialpad"/>
        </Link>
      </Row>
      <ProfileContainer user={auth.user}/>
    </Row>
  );
};

const mapStateToProps = (state: RootState) => {
  return {
    pathname: state.routing.location!.pathname,
    auth: state.auth,
  };
};

export default connect(mapStateToProps)(TopMenuContainer);
