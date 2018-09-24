import * as classNames from 'classnames';
import * as React from 'react';
import {connect} from 'react-redux';
import {Link} from 'react-router-dom';
import {getLogoPath, routes} from '../../../app/routes';
import {Row, RowCenter} from '../../../components/layouts/row/Row';
import {Logo} from '../../../components/logo/Logo';
import {RootState} from '../../../reducers/rootReducer';
import {User} from '../../../state/domain-models/user/userModels';
import {ClassNamed, WithChildren} from '../../../types/Types';
import {getUser} from '../../auth/authSelectors';
import './TopMenuContainer.scss';

interface StateToProps extends ClassNamed, WithChildren {
  user: User;
}

const TopMenu = ({children, className, user: {organisation}}: StateToProps) => (
  <Row className={classNames('SelectionMenuWrapper space-between', className)}>
    <Row className="SelectionMenu">
      {children}
    </Row>
    <RowCenter>
      <Link className="Logo" to={routes.home}>
        <Logo src={getLogoPath(organisation.slug)} className="small"/>
      </Link>
    </RowCenter>
  </Row>
);

const mapStateToProps = ({auth}: RootState): StateToProps => ({
  user: getUser(auth),
});

export const TopMenuContainer =
  connect<StateToProps, {}>(mapStateToProps)(TopMenu);
