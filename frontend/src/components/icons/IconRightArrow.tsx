import IconButton from 'material-ui/IconButton';
import NavigationArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import * as React from 'react';
import {colors} from '../../app/colors';
import {ClassNamed, Clickable} from '../../types/Types';
import './Icons.scss';

const iconSize: React.CSSProperties = {
  width: 30,
  height: 30,
  color: colors.black,
};

const style: React.CSSProperties = {
  padding: 0,
  ...iconSize,
};

export const IconRightArrow = ({className, onClick}: ClassNamed & Partial<Clickable>) => (
  <IconButton
    className={className}
    onClick={onClick}
    iconStyle={iconSize}
    style={style}
  >
    <NavigationArrowDropRight/>
  </IconButton>
);
