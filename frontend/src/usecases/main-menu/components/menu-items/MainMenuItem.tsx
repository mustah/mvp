import {default as classNames} from 'classnames';
import * as React from 'react';
import {colors} from '../../../../app/colors';
import {iconStyle} from '../../../../app/themes';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Normal} from '../../../../components/texts/Texts';
import {Selectable, WithChildren} from '../../../../types/Types';
import './MainMenuItem.scss';
import SvgIconProps = __MaterialUI.SvgIconProps;

export const mainMenuIconProps: SvgIconProps = {
  style: {...iconStyle, width: 26, height: 26},
  color: colors.black,
  className: 'MainMenuItem-icon',
};

export interface MenuItemProps extends Selectable, WithChildren {
  name: string;
  icon: React.ReactElement<any>;
  fontClassName?: string;
}

export const MainMenuItem = ({children, name, fontClassName = 'Large', icon, isSelected}: MenuItemProps) => (
  <RowMiddle className={classNames('MainMenuItem', {isSelected})}>
    {icon}
    <Normal className={classNames('first-uppercase', fontClassName)}>{name}</Normal>
    {children}
  </RowMiddle>
);
