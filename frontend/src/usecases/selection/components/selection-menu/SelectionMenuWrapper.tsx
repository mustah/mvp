import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {bindActionCreators} from 'redux';
import {companyLogo, routes} from '../../../../app/routes';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Logo} from '../../../../components/logo/Logo';
import {RootState} from '../../../../reducers/rootReducer';
import {ClassNamed, OnClick} from '../../../../types/Types';
import {logout} from '../../../auth/authActions';
import {User} from '../../../auth/authModels';
import {getUser} from '../../../auth/authSelectors';
import {Profile} from '../profile/Profile';
import './SelectionMenuWrapper.scss';

interface StateToProps extends ClassNamed {
  user: User;
  children?: React.ReactNode;
}

interface DispatchToProps {
  logout: OnClick;
}

interface OwnProps {
  className: string;
}

const SearchMenuWrapperComponent = (props: StateToProps & DispatchToProps) => {
  const {children, className, user, logout} = props;
  const companyLogoPath = companyLogo[user.company.id];
  const logo = companyLogoPath ?  <Logo src={companyLogoPath} className="small"/> :
    <Logo src="elvaco_logo.png" className="small" />;
  return (
    <Row className={classNames('SelectionMenuWrapper', className)}>
      <Row className="SelectionMenu">
        {children}
      </Row>
      <RowCenter>
        <Link className="Logo" to={routes.home}>
          {logo}
        </Link>
      </RowCenter>
      <Row>
        <Profile user={user!} logout={logout}/>
      </Row>
    </Row>
  );
};

const mapStateToProps = ({auth}: RootState): StateToProps => {
  return {
    user: getUser(auth),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const SearchMenuWrapper =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(SearchMenuWrapperComponent);
