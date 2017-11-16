import * as classNames from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {ClassNamed} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {Row} from '../../../common/components/layouts/row/Row';
import {Logo} from '../../../common/components/logo/Logo';
import './SelectionMenuWrapper.scss';

interface Props extends ClassNamed {
  children?: React.ReactNode;
}

export const SearchMenuWrapper = (props: Props) => {
  const {children, className} = props;

  return (
    <Row className={classNames('SelectionMenuWrapper', className)}>
      <Row className="SelectionMenu">
        {children}
      </Row>
      <Row>
        <Link className="Logo" to={routes.home}>
          <Logo className="small"/>
        </Link>
      </Row>
    </Row>
  );
};
