import * as classNames from 'classnames';
import * as React from 'react';
import './Texts.scss';

export const Normal = (props) => {
  const {className, children} = props;
  return (
    <div className={classNames('Normal', className)}>{children}</div>
  );
};

export const Bold = (props) => (<Normal {...props} className={classNames('Bold', props.className)}/>);

export const Large = (props) => (<Normal {...props} className={classNames('Large', props.className)}/>);

export const Small = (props) => (<Normal {...props} className={classNames('Small', props.className)}/>);

export const Xlarge = (props) => (<Normal {...props} className={classNames('Xlarge', props.className)}/>);

export const Xsmall = (props) => (<Normal {...props} className={classNames('Xsmall', props.className)}/>);
