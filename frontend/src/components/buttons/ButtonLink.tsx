import 'ButtonLink.scss';
import * as classNames from 'classnames';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';

interface Props extends Clickable {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
}

export const ButtonLink = (props: Props) => {
  const {className, children, onClick} = props;
  return (
    <Row className={classNames('ButtonLink', className)} onClick={onClick}>
      <Normal className="first-uppercase">{children}</Normal>
    </Row>
  );
};
