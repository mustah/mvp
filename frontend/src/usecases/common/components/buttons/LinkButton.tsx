import * as classNames from 'classnames';
import 'LinkButton.scss';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {Normal} from '../texts/Texts';
import {Row} from '../layouts/row/Row';

interface Props extends Clickable {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
}

export const LinkButton = (props: Props) => {
  const {className, children, onClick} = props;
  return (
    <Row className={classNames('LinkButton first-uppercase', className)} onClick={onClick}>
      <Normal>{children}</Normal>
    </Row>
  );
};
