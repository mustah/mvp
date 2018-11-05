import * as React from 'react';

declare module 'react' {

  type SetStateAction<S> = S | ((prevState: S) => S);
  type Dispatch<A> = (value: A) => void;
  type Reducer<S, A> = (prevState: S, action: A) => S;
  type EffectCallback = () => (void | (() => void));
  type InstanceFactory<T> = <T>() => T;
  type IdentityCheckInputList = ReadonlyArray<any>;

  function useState<S>(initialState: S | InstanceFactory<S>): [S, Dispatch<SetStateAction<S>>];

  function useEffect(effect: EffectCallback, inputs?: IdentityCheckInputList): void;

  function useReducer<S, A>(reducer: Reducer<S, A>, initialState: S): [S, Dispatch<A>];

  function useCallback<F extends (...args: never[]) => any>(
    callback: F,
    inputs?: IdentityCheckInputList
  ): F;

  function useMemo<T>(factory: InstanceFactory<T>, inputs?: IdentityCheckInputList): T;

  function useRef<T extends {}>(initialValue?: T): React.Ref<T>;

  function useImperativeMethods<T>(
    ref: React.Ref<T>,
    factory: InstanceFactory<T>,
    inputs?: IdentityCheckInputList
  ): void;

  const useMutationEffect: typeof useEffect;
  const useLayoutEffect: typeof useEffect;
}
