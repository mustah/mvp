import {default as classNames} from 'classnames';
import {important} from 'csx';
import {SvgIconProps} from 'material-ui';
import FlatButton from 'material-ui/FlatButton';
import NavigationChevronLeft from 'material-ui/svg-icons/navigation/chevron-left';
import NavigationChevronRight from 'material-ui/svg-icons/navigation/chevron-right';
import * as React from 'react';
import {style} from 'typestyle';
import {colors} from '../../../../app/colors';
import {boxShadow, dividerBorder, iconStyle} from '../../../../app/themes';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RowRight} from '../../../../components/layouts/row/Row';
import {translate} from '../../../../services/translationService';
import {OnClick} from '../../../../types/Types';
import './MainMenuToggleIcon.scss';

interface Props extends ThemeContext {
  onClick: OnClick;
  isSideMenuOpen: boolean;
}

const buttonStyle: React.CSSProperties = {
  minWidth: 44,
  height: 44,
  borderRadius: 44 / 2,
  border: dividerBorder,
  backgroundColor: colors.white,
  boxShadow,
};

export const MainMenuToggleIcon = withCssStyles(({cssStyles: {primary}, onClick, isSideMenuOpen}: Props) => {
  const iconsProps: SvgIconProps = {
    style: {
      ...iconStyle,
      cursor: 'pointer',
    },
    color: primary.fg,
    hoverColor: primary.fgHover,
  };

  const renderArrow = isSideMenuOpen
    ? (<NavigationChevronLeft {...iconsProps}/>)
    : (<NavigationChevronRight {...iconsProps}/>);

  const buttonClassName = style({
    $nest: {
      '&:hover .MainMenuToggleIcon-Button': {backgroundColor: important(primary.bgHover)},
      '&:hover .MainMenuToggleIcon-Button svg': {fill: important(colors.black)},
    }
  });

  return (
    <RowRight className={classNames('MainMenuToggleIcon', {isSideMenuOpen}, buttonClassName)}>
      <FlatButton
        className="MainMenuToggleIcon-Button"
        onClick={onClick}
        icon={renderArrow}
        label={isSideMenuOpen ? translate('hide menu') : null}
        labelPosition="before"
        labelStyle={{color: primary.fgHover}}
        style={buttonStyle}
      />
    </RowRight>
  );
});
