import {default as classNames} from 'classnames';
import {important} from 'csx';
import * as React from 'react';
import {style} from 'typestyle';
import {useToggleVisibility} from '../../../hooks/toogleVisibilityHook';
import {Clickable, Selectable} from '../../../types/Types';
import {MainMenuItem} from '../../../usecases/main-menu/components/menu-items/MainMenuItem';
import {ThemeContext, withCssStyles} from '../../hoc/withThemeProvider';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {Column} from '../column/Column';
import {Row, RowMiddle} from '../row/Row';
import {FoldableProps} from './Foldable';
import './Foldable.scss';
import './FoldableMainMenuItem.scss';

interface Props extends FoldableProps, Required<Selectable> {
  icon: React.ReactElement<any>;
}

const FoldableTitle = withCssStyles(({
  cssStyles: {primary},
  fontClassName,
  icon,
  isSelected,
  isVisible,
  onClick,
  title,
}: Props & Clickable & ThemeContext) => {
  const className = style({
    $nest: {
      '&:hover': {backgroundColor: primary.bgHover},
      '&.isSelected': {backgroundColor: primary.bgActive},
      '&.isSelected .Normal': {color: important(primary.fgActive), fontWeight: 'bold'},
      '&.isSelected .MainMenuItem-icon': {fill: important(primary.fgActive)},
    },
  });
  return (
    <RowMiddle onClick={onClick} className={classNames('Foldable-title', 'clickable', {isSelected}, className)}>
      <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
      <MainMenuItem name={title} fontClassName={fontClassName} icon={icon}/>
    </RowMiddle>
  );
});

export const FoldableMainMenuItem = ({
  icon,
  children,
  className,
  containerClassName,
  fontClassName,
  isSelected,
  title,
  isVisible: initialVisibility = true
}: Props) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <FoldableTitle
        fontClassName={fontClassName}
        icon={icon}
        isSelected={isSelected}
        onClick={showHide}
        title={title}
      />
      <Row className={classNames('Foldable-content', className, {isVisible})}>
        {children}
      </Row>
    </Column>
  );
};
