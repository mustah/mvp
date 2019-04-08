import {default as classNames} from 'classnames';
import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {topMenuInnerDivStyle, topMenuItemDivStyle} from '../../../app/themes';
import {ClassNamed, Clickable, WithChildren} from '../../../types/Types';

interface Props extends Clickable, ClassNamed, WithChildren {
  leftIcon?: React.ReactElement<any>;
  to: string;
  target?: string;
}

export const LinkMenuItem = ({children, leftIcon, to, target, onClick}: Props) => (
  <Link to={to} className="link" target={target}>
    <MenuItem
      className="first-uppercase"
      leftIcon={leftIcon}
      innerDivStyle={topMenuInnerDivStyle}
      onClick={onClick}
      style={topMenuItemDivStyle}
    >
      {children}
    </MenuItem>
  </Link>
);

export const HrefMenuItem = ({children, className, leftIcon, to, target, onClick}: Props) => (
  <a href={to} target={target} className="link">
    <MenuItem
      className={classNames('first-uppercase', className)}
      leftIcon={leftIcon}
      innerDivStyle={topMenuInnerDivStyle}
      onClick={onClick}
      style={topMenuItemDivStyle}
    >
      {children}
    </MenuItem>
  </a>
);
