import {default as classNames} from 'classnames';
import {important} from 'csx';
import * as React from 'react';
import {style} from 'typestyle';
import {colors} from '../../../../app/colors';
import {iconStyle} from '../../../../app/themes';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
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

export interface MenuItemProps extends Selectable, ThemeContext, WithChildren {
  name: string;
  icon: React.ReactElement<any>;
  fontClassName?: string;
}

export const MainMenuItem =
  withCssStyles(({cssStyles: {primary}, children, name, fontClassName = 'Large', icon, isSelected}: MenuItemProps) => {
    const className = style({
      $nest: {
        '&:hover': {backgroundColor: primary.bgHover},
        '&.isSelected:hover': {backgroundColor: primary.bgActive},
        '&.isSelected': {backgroundColor: primary.bgActive, color: primary.fgActive},
        '&.MainMenuItem-icon': {fill: important(primary.fgActive)},
      },
    });
    return (
      <RowMiddle className={classNames('MainMenuItem', {isSelected}, className)}>
        {icon}
        <Normal className={classNames('first-uppercase', fontClassName)}>{name}</Normal>
        {children}
      </RowMiddle>
    );
  });
