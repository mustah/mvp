import * as classNames from 'classnames';
import * as React from 'react';
import './Texts.scss';

interface TextProps {
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
}

export const Normal = (props: TextProps) => {
  const {className, children} = props;
  return (
    <span {...props} className={classNames('Normal', className)}>{children}</span>
  );
};

export const Bold = (props: TextProps) => (<Normal {...props} className={classNames('Bold', props.className)}/>);

export const Large = (props: TextProps) => (<Normal {...props} className={classNames('Large', props.className)}/>);

export const Small = (props: TextProps) => (<Normal {...props} className={classNames('Small', props.className)}/>);

export const Xlarge = (props: TextProps) => (<Normal {...props} className={classNames('Xlarge', props.className)}/>);

export const Xsmall = (props: TextProps) => (<Normal {...props} className={classNames('Xsmall', props.className)}/>);
