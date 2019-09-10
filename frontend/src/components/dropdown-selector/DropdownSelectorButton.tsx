import {important} from 'csx';
import * as React from 'react';
import {classes, style as typestyle} from 'typestyle';
import {colors} from '../../app/colors';
import {ClassNamed, ClickableEventHandler, Styled} from '../../types/Types';
import {ThemeContext, withCssStyles} from '../hoc/withThemeProvider';
import {IconDropDown} from '../icons/IconDropDown';
import {Row, RowMiddle} from '../layouts/row/Row';
import {FirstUpper} from '../texts/Texts';
import './DropdownSelectorButton.scss';

interface Props extends ClassNamed, ClickableEventHandler, ThemeContext, Styled {
  isOpen: boolean;
  text: string;
}

export const DropDownSelectorButton = withCssStyles(({
  className,
  cssStyles: {primary, secondary},
  isOpen,
  onClick,
  style,
  text,
}: Props) => {
  const classNames = typestyle({
    color: colors.black,
    backgroundColor: secondary.bg,
    cursor: 'pointer',
    $nest: {
      '&:hover': {backgroundColor: primary.bgHover},
      '&:hover.DropdownSelector-Grid': {border: `2px solid ${primary.bgActive}`},
      '&.isOpen': {
        border: `2px solid ${primary.bg}`,
        backgroundColor: important(primary.bgHover),
      },
    }
  });

  return (
    <Row className="DropdownSelector">
      <div
        onClick={onClick}
        className={classes('DropdownSelector-Text', className, {isOpen}, classNames)}
        style={style}
      >
        <RowMiddle>
          <FirstUpper>{text}</FirstUpper>
          <IconDropDown/>
        </RowMiddle>
      </div>
    </Row>
  );
});
