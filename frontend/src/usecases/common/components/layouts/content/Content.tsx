import * as classNames from 'classnames';
import 'Content.scss';
import * as React from 'react';
import {ClassNamed} from '../../../../../types/Types';

export const Content: React.SFC<ClassNamed> = props => (
  <div className={classNames('Content', props.className)}>
    {props.children}
  </div>
);
