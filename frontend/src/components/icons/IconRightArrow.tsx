import IconButton from 'material-ui/IconButton';
import NavigationArrowDropRight from 'material-ui/svg-icons/navigation-arrow-drop-right';
import * as React from 'react';
import {colors} from '../../app/themes';
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

type Props = Clickable & ClassNamed;

export const IconRightArrow = ({onClick, className}: Props) => (
  <IconButton
    className={className}
    style={style}
    iconStyle={iconSize}
    onClick={onClick}
  >
    <NavigationArrowDropRight/>
  </IconButton>
);
