import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layoutModels';
import './Layout.scss';

export const Layout: React.StatelessComponent<LayoutProps> = (props) => {
  if (props.hide) {
    return null;
  }
  return (
    <div className={classNames('Layout', props.className)}>
      {props.children}
    </div>
  );
};
