import {DropDownMenu, MenuItem} from 'material-ui';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {borderRadius, colors, fontSize, menuItemStyle} from '../../app/themes';
import {firstUpperTranslated} from '../../services/translationService';
import {Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {Children, OnClick, Styled, WithChildren} from '../../types/Types';
import {Period} from '../dates/dateModels';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';
import './DropdownMenu.scss';
import SvgIconProps = __MaterialUI.SvgIconProps;

const height = 32;

const menu: {[name: string]: React.CSSProperties} = {
  iconStyle: {
    fill: colors.lightBlack,
    height,
    width: 36,
    right: 0,
    top: 0,
    padding: 0,
  },
  labelStyle: {
    height,
    lineHeight: 1,
    paddingRight: 0,
    paddingLeft: 8,
    fontSize: 14,
    display: 'flex',
    alignItems: 'center',
    width: 210,
  },
  style: {
    height,
    width: 210,
    fontSize: fontSize.normal,
    border: `2px solid ${colors.borderColor}`,
    marginLeft: 24,
    marginBottom: 16,
    borderWidth: 1,
    borderRadius,
  },
  selectedMenuItemStyle: {color: colors.blue},
  underlineStyle: {border: 'none'},
};

export interface DropdownMenuProps extends WithChildren, Styled {
  disabled?: boolean;
  labelStyle?: React.CSSProperties;
  listStyle?: React.CSSProperties;
  menuItems: MenuItemProps[];
  value?: Period | string;
  IconButton?: React.ComponentType<SvgIconProps>;
}

export interface MenuItemProps {
  checked?: boolean;
  label: string;
  onClick: OnClick;
  primaryText: string;
  value: Period | string;
}

export const DropdownMenu = ({
  children,
  IconButton = IconCalendar,
  labelStyle,
  listStyle,
  menuItems,
  value,
  style
}: DropdownMenuProps) => {

  const renderedMenuItems = menuItems.map(({primaryText, label, value, onClick}: MenuItemProps) => (
    <MenuItem
      className="DropdownMenu-MenuItem"
      key={primaryText}
      label={label}
      primaryText={primaryText}
      style={menuItemStyle}
      value={value}
      onClick={onClick}
    />
  ));

  const menuStyle = {...menu.style, ...style};
  const menuLabelStyle = {...menu.labelStyle, ...labelStyle};

  return (
    <Row className="DropdownMenu">
      <DropDownMenu
        className="DropdownMenu-dropdown"
        iconButton={<IconButton className="IconButton"/>}
        iconStyle={menu.iconStyle}
        maxHeight={300}
        labelStyle={menuLabelStyle}
        listStyle={listStyle}
        selectedMenuItemStyle={menu.selectedMenuItemStyle}
        style={menuStyle}
        underlineStyle={menu.underlineStyle}
        value={value}
      >
        {renderedMenuItems}
      </DropDownMenu>
      {children}
    </Row>
  );
};

const multiSelectLabelStyle: React.CSSProperties = {
  ...menu.labelStyle,
  height: 38,
  width: 168,
};

const multiSelectStyle: React.CSSProperties = {
  ...menu.style,
  marginLeft: 0,
  marginBottom: 0,
  borderColor: colors.borderColor,
};

const hintStyle: React.CSSProperties = {bottom: 5, left: 8, color: colors.lightBlack};

export interface MultiSelectDropdownMenuProps {
  selectedQuantities: Quantity[];
  changeQuantities: (event, index, values) => void;
  children?: Children;
}

export const MultiSelectDropdownMenu =
  ({children, changeQuantities, selectedQuantities}: MultiSelectDropdownMenuProps) => (
    <Row className="DropdownMenu">
      <SelectField
        autoWidth={true}
        className="DropdownMenu-dropdown"
        hintStyle={hintStyle}
        hintText={firstUpperTranslated('select quantities')}
        iconStyle={menu.iconStyle}
        labelStyle={multiSelectLabelStyle}
        multiple={true}
        onChange={changeQuantities}
        selectedMenuItemStyle={menu.selectedMenuItemStyle}
        style={multiSelectStyle}
        value={selectedQuantities}
        underlineStyle={menu.underlineStyle}
      >
        {children}
      </SelectField>
    </Row>
  );
