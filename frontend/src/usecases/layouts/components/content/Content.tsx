import * as classNames from 'classnames';
import * as React from 'react';

export const Content = props =>
  (
    <div className={classNames('Content', props.className)}>
      {props.children}
    </div>
  );
