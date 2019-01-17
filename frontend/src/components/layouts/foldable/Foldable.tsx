import {default as classNames} from 'classnames';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {Callback, ClassNamed, Clickable, PathNamed, WithChildren} from '../../../types/Types';
import {MainMenuItem} from '../../../usecases/main-menu/components/menu-items/MainMenuItem';
import {IconRightArrow} from '../../icons/IconRightArrow';
import {BoldFirstUpper} from '../../texts/Texts';
import {Column} from '../column/Column';
import {Row, RowMiddle} from '../row/Row';
import './Foldable.scss';

interface Visible {
  isVisible: boolean;
}

interface FoldableProps extends ClassNamed, WithChildren, Partial<Visible> {
  title: string;
  containerClassName?: string;
  fontClassName?: string;
}

interface ToggleVisibilityProps extends Visible {
  showHide: Callback;
}

const useToggleVisibility = (initialState: boolean): ToggleVisibilityProps => {
  const [isVisible, toggle] = React.useState<boolean>(initialState);
  const showHide = () => toggle(!isVisible);
  return {isVisible, showHide};
};

export const Foldable = ({
  children,
  className,
  containerClassName,
  fontClassName = 'Medium',
  title,
  isVisible: initialVisibility = true
}: FoldableProps) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle onClick={showHide} className={classNames('Foldable-title', 'clickable')}>
        <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
        <BoldFirstUpper className={fontClassName}>{title}</BoldFirstUpper>
      </RowMiddle>
      <Row className={classNames('Foldable-content', className, {isVisible})}>
        {children}
      </Row>
    </Column>
  );
};

export const FoldableMenuItem = (props: FoldableProps) =>
  <Foldable containerClassName="FoldableMenuItem" fontClassName="Normal" {...props}/>;

interface FoldableMainMenuItemProps extends FoldableProps, PathNamed, Partial<Clickable> {
  icon: React.ReactElement<any>;
  isSelected: boolean;
  linkTo: string;
}

export const FoldableMainMenuItem = ({
  icon,
  children,
  className,
  containerClassName,
  fontClassName,
  isSelected,
  linkTo,
  onClick,
  pathName,
  title,
  isVisible: initialVisibility = true
}: FoldableMainMenuItemProps) => {
  const {isVisible, showHide} = useToggleVisibility(initialVisibility);
  const selected = {isSelected};
  const visible = {isVisible};

  return (
    <Column className={classNames('Foldable', containerClassName)}>
      <RowMiddle className={classNames('Foldable-title', 'clickable', selected)}>
        <IconRightArrow onClick={showHide} className={classNames('Foldable-arrow', visible)}/>
        <Link to={linkTo} className="link" onClick={onClick}>
          <MainMenuItem
            name={title}
            fontClassName={fontClassName}
            icon={icon}
          />
        </Link>
      </RowMiddle>
      <Row className={classNames('Foldable-content', className, visible)}>
        {children}
      </Row>
    </Column>
  );
};
