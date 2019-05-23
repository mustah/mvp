import {default as classNames} from 'classnames';
import MenuItem from 'material-ui/MenuItem';
import * as React from 'react';
import {topMenuInnerDivStyle, topMenuItemDivStyle} from '../../../app/themes';
import {Link, LinkProps} from '../../../components/links/Link';
import {Clickable, WithChildren} from '../../../types/Types';

interface Props extends Clickable, WithChildren, LinkProps {
  leftIcon?: React.ReactElement<any>;
}

export const TopMenuLinkItem = ({children, className, leftIcon, to, target, onClick}: Props) => (
  <Link to={to} target={target}>
    <MenuItem
      className={classNames('first-uppercase', className)}
      leftIcon={leftIcon}
      innerDivStyle={topMenuInnerDivStyle}
      onClick={onClick}
      style={topMenuItemDivStyle}
    >
      {children}
    </MenuItem>
  </Link>
);
