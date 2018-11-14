import {default as classNames} from 'classnames';
import * as React from 'react';
import './Texts.scss';

interface TextProps {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
  style?: React.CSSProperties;
  title?: string;
}

export const Normal = ({className, children, ...props}: TextProps) =>
  <div {...props} className={classNames('Normal', className)}>{children}</div>;

export const FirstUpper = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('first-uppercase', className)}/>;

export const Bold = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Bold', className)}/>;

export const BoldFirstUpper = ({className, ...props}: TextProps) =>
  <Bold {...props} className={classNames('first-uppercase', className)}/>;

export const Large = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Large', className)}/>;

export const Medium = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Medium', className)}/>;

export const Small = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Small', className)}/>;

export const Xlarge = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Xlarge', className)}/>;

export const Xsmall = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Xsmall', className)}/>;

export const Error = ({className, ...props}: TextProps) =>
  <Normal {...props} className={classNames('Error', className)}/>;
