import * as classNames from 'classnames';
import * as React from 'react';
import './Texts.scss';

export const Normal = (props) => {
  const {className, children} = props;
  return (
    <div className={classNames('Normal', className)}>{children}</div>
  );
};

export const Bold = (props) => (<Normal className="Bold" {...props}/>);

export const Large = (props) => (<Normal className="Large" {...props}/>);

export const Small = (props) => (<Normal className="Small" {...props}/>);

export const Xlarge = (props) => (<Normal className="Xlarge" {...props}/>);

export const Xsmall = (props) => (<Normal className="Xsmall" {...props}/>);
