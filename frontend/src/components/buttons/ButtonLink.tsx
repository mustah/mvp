import {default as classNames} from 'classnames';
import * as React from 'react';
import {Clickable} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {BoldFirstUpper} from '../texts/Texts';
import './ButtonLink.scss';

interface Props extends Clickable {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
  colorClassName?: string;
}

export const ButtonLink = ({className, children, onClick, colorClassName}: Props) => (
  <Row className={classNames('ButtonLink', className)} onClick={onClick}>
    <BoldFirstUpper className={colorClassName}>{children}</BoldFirstUpper>
  </Row>
);
