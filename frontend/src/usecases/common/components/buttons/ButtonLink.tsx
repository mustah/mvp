import * as classNames from 'classnames';
import 'ButtonLink.scss';
import * as React from 'react';
import {Clickable} from '../../../../types/Types';
import {Normal} from '../texts/Texts';
import {Row} from '../layouts/row/Row';

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
