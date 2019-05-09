import {default as classNames} from 'classnames';
import * as React from 'react';
import {Clickable, Omit, Styled} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {BoldFirstUpper, FirstUpper} from '../texts/Texts';
import './ButtonLink.scss';

interface Props extends Clickable, Styled {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
  textClassName?: string;
}

export const ButtonLink = ({className, children, onClick, textClassName, style}: Props) => (
  <Row className={classNames('ButtonLink', className)} onClick={onClick} style={style}>
    <BoldFirstUpper className={textClassName}>{children}</BoldFirstUpper>
  </Row>
);

export const ButtonLinkBlue = ({className, children, onClick}: Omit<Props, 'textClassName'>) => (
  <Row className={classNames('ButtonLink blue', className)} onClick={onClick}>
    <FirstUpper>{children}</FirstUpper>
  </Row>
);

export const ButtonLinkRed = ({className, children, onClick}: Omit<Props, 'textClassName'>) => (
  <Row className={classNames('ButtonLink red', className)} onClick={onClick}>
    <FirstUpper>{children}</FirstUpper>
  </Row>
);
