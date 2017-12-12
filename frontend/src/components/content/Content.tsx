import * as React from 'react';
import './Content.scss';
import classNames = require('classnames');

interface Props {
  children: React.ReactElement<any>;
  hasContent: boolean;
  noContentText: string;
  className?: string;
}

export const Content = (props: Props) => {
  const {className, hasContent, noContentText, children} = props;

  if (!hasContent) {
    return (<h2 className={classNames('Content first-uppercase', className)}>{noContentText}</h2>);
  } else {
    return children;
  }
};
