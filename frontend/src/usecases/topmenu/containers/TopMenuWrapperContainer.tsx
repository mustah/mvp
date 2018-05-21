import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {getLogoPath, routes} from '../../../app/routes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {Logo} from '../../../components/logo/Logo';
import {RootState} from '../../../reducers/rootReducer';
import {User} from '../../../state/domain-models/user/userModels';
import {ClassNamed} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {OnLogout} from '../../auth/authModels';
import {getUser} from '../../auth/authSelectors';
import {Profile} from '../components/profile/Profile';
import './TopMenuWrapperContainer.scss';

interface StateToProps extends ClassNamed {
  user: User;
  children?: React.ReactNode;
}

interface DispatchToProps {
  logout: OnLogout;
}

interface OwnProps {
  className: string;
}

const TopMenuWrapper = ({children, className, user, logout}: StateToProps & DispatchToProps) => (
  <Row className={classNames('SelectionMenuWrapper', className)}>
    <Row className="SelectionMenu">
      {children}
    </Row>
    <RowCenter>
      <Link className="Logo" to={routes.home}>
        <Logo src={getLogoPath(user.organisation.slug)} className="small"/>
      </Link>
    </RowCenter>
    <Row>
      <Profile user={user} logout={logout}/>
    </Row>
  </Row>
);

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  user: getUser(auth),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const TopMenuWrapperContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(TopMenuWrapper);
